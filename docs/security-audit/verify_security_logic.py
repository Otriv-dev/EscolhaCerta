#!/usr/bin/env python3
"""Compila classes reais contra stubs e verifica SQL em SQLite; não substitui Maven/MySQL."""
from pathlib import Path
import tempfile,subprocess,re,sqlite3,time,hashlib,concurrent.futures,json
ROOT=Path(__file__).resolve().parents[2]
with tempfile.TemporaryDirectory(prefix='escolhacerta-check-') as folder:
 work=Path(folder);src=work/'src';src.mkdir()
 stubs={
 'jakarta/servlet/ServletException.java':'package jakarta.servlet; public class ServletException extends Exception {}',
 'jakarta/servlet/FilterChain.java':'package jakarta.servlet; public interface FilterChain {void doFilter(jakarta.servlet.http.HttpServletRequest r,jakarta.servlet.http.HttpServletResponse s) throws ServletException,java.io.IOException;}',
 'jakarta/servlet/http/HttpServletRequest.java':'package jakarta.servlet.http; public interface HttpServletRequest {String getRequestURI(); String getContextPath(); String getMethod(); String getRemoteAddr(); String getParameter(String name);}',
 'jakarta/servlet/http/HttpServletResponse.java':'package jakarta.servlet.http; public interface HttpServletResponse {void setHeader(String k,String v); void sendError(int status) throws java.io.IOException;}',
 'org/springframework/stereotype/Component.java':'package org.springframework.stereotype; public @interface Component {}',
 'org/springframework/dao/DataAccessException.java':'package org.springframework.dao; public class DataAccessException extends RuntimeException {}',
 'org/springframework/dao/DuplicateKeyException.java':'package org.springframework.dao; public class DuplicateKeyException extends DataAccessException {}',
 'org/springframework/web/filter/OncePerRequestFilter.java':'package org.springframework.web.filter; public abstract class OncePerRequestFilter {protected abstract void doFilterInternal(jakarta.servlet.http.HttpServletRequest r,jakarta.servlet.http.HttpServletResponse s,jakarta.servlet.FilterChain c) throws jakarta.servlet.ServletException,java.io.IOException;}',
 'org/springframework/jdbc/core/JdbcTemplate.java':'''package org.springframework.jdbc.core;
 import java.util.*;
 public class JdbcTemplate {
  private static class Row {long start;int count;Row(long start){this.start=start;}}
  private final Map<String,Row> rows=new HashMap<>();
  public synchronized int update(String sql,Object... p){
   if(sql.startsWith("DELETE")){rows.entrySet().removeIf(e->e.getValue().start<((Number)p[0]).longValue());return 0;}
   if(sql.startsWith("INSERT")){String key=(String)p[0];if(rows.containsKey(key))throw new org.springframework.dao.DuplicateKeyException();rows.put(key,new Row(((Number)p[1]).longValue()));return 1;}
   if(sql.contains("SET started_at")){Row r=rows.get((String)p[1]);if(r!=null&&r.start<((Number)p[2]).longValue()){r.start=((Number)p[0]).longValue();r.count=0;return 1;}return 0;}
   Row r=rows.get((String)p[0]);if(r!=null&&r.count<10){r.count++;return 1;}return 0;
  }
  public synchronized void expireAll(){rows.values().forEach(r->r.start=0);}
 }
 '''}
 for rel,code in stubs.items():p=src/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(code)
 for name in ['PasswordPolicy','AccountLoginThrottle','RateLimitFilter']:
  p=src/'br/com/escolhacerta/security'/f'{name}.java';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes((ROOT/'src/main/java/br/com/escolhacerta/security'/f'{name}.java').read_bytes())
 probe=r'''import java.nio.file.*;import java.net.*;import javax.tools.*;import java.util.*;import java.util.concurrent.*;
public class Probe {
 static void check(boolean value,String label){if(!value)throw new AssertionError(label);System.out.println("PASS: "+label);}
 public static void main(String[] args)throws Exception {
  Path src=Path.of(args[0]),classes=src.resolveSibling("classes");Files.createDirectories(classes);
  var files=new ArrayList<String>(List.of("-d",classes.toString()));try(var stream=Files.walk(src)){stream.filter(p->p.toString().endsWith(".java")).forEach(p->files.add(p.toString()));}
  if(ToolProvider.getSystemJavaCompiler().run(null,null,null,files.toArray(String[]::new))!=0)throw new AssertionError("compile");
  var loader=new URLClassLoader(new URL[]{classes.toUri().toURL()});var policy=loader.loadClass("br.com.escolhacerta.security.PasswordPolicy").getMethod("validate",String.class);
  for(String weak:List.of("aaaaaaaaaaaa","123456789012","aaaaaaaaaaaaaaa","123456789012345","passwordpassword","Á".repeat(37))){try{policy.invoke(null,weak);throw new AssertionError("weak accepted");}catch(java.lang.reflect.InvocationTargetException ex){check(ex.getCause() instanceof IllegalArgumentException,"senha previsível/fora do limite rejeitada");}}
  policy.invoke(null,"Meu jardim recebe luz de manhã!");policy.invoke(null,"TesteSeguro!2026Validacao");check(true,"passphrase Unicode e fixture de teste aceitas");
  var jdbcClass=loader.loadClass("org.springframework.jdbc.core.JdbcTemplate");var jdbc=jdbcClass.getConstructor().newInstance();var accountClass=loader.loadClass("br.com.escolhacerta.security.AccountLoginThrottle");var first=accountClass.getConstructor(jdbcClass).newInstance(jdbc);var second=accountClass.getConstructor(jdbcClass).newInstance(jdbc);var allowed=accountClass.getMethod("allowed",String.class);
  ExecutorService pool=Executors.newFixedThreadPool(8);try{var calls=new ArrayList<Callable<Boolean>>();for(int i=0;i<40;i++){final int n=i;calls.add(()->(boolean)allowed.invoke(n%2==0?first:second,n%2==0?"admin@example.com":"ADMIN@example.com"));}int admitted=0;for(var f:pool.invokeAll(calls))if(f.get())admitted++;check(admitted==10,"classe real: 40 chamadas concorrentes, duas instâncias, mesma conta normalizada, exatamente 10 aceitas (JDBC stub)");}finally{pool.shutdownNow();}
  check(!(boolean)allowed.invoke(accountClass.getConstructor(jdbcClass).newInstance(jdbc),"admin@example.com"),"nova instância não renova quota");jdbcClass.getMethod("expireAll").invoke(jdbc);check((boolean)allowed.invoke(first,"admin@example.com"),"janela expirada permite autenticar novamente");
  var filterClass=loader.loadClass("br.com.escolhacerta.security.RateLimitFilter");var filter=filterClass.getConstructor(accountClass).newInstance(first);var req=loader.loadClass("jakarta.servlet.http.HttpServletRequest");var res=loader.loadClass("jakarta.servlet.http.HttpServletResponse");var chain=loader.loadClass("jakarta.servlet.FilterChain");var method=filterClass.getDeclaredMethod("doFilterInternal",req,res,chain);method.setAccessible(true);
  final int[] passed={0},blocked={0};final String[] path={"/blog"}, verb={"GET"};
  Object rq=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{req},(o,m,a)->switch(m.getName()){case "getRequestURI"->path[0];case "getContextPath"->"";case "getMethod"->verb[0];case "getRemoteAddr"->"192.0.2.1";case "getParameter"->"admin@example.com";default->null;});
  Object rs=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{res},(o,m,a)->{if(m.getName().equals("sendError"))blocked[0]++;return null;});Object ch=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{chain},(o,m,a)->{passed[0]++;return null;});
  for(int i=0;i<121;i++)method.invoke(filter,rq,rs,ch);check(passed[0]==120&&blocked[0]==1,"classe real: GET 121 bloqueado após 120 leituras");verb[0]="HEAD";method.invoke(filter,rq,rs,ch);check(blocked[0]==2,"HEAD não contorna o orçamento de leituras");verb[0]="GET";path[0]="/actuator/health";method.invoke(filter,rq,rs,ch);check(passed[0]==121&&blocked[0]==2,"health não bloqueado pela quota de páginas");
 }
}'''
 (work/'Probe.java').write_text(probe)
 subprocess.run(['java',str(work/'Probe.java'),str(src)],check=True)
 # Instruções SQL exatas em conexões independentes, sem mocks.
 text=(ROOT/'src/main/java/br/com/escolhacerta/security/AccountLoginThrottle.java').read_text()
 cleanup,insert,reset,admit=re.findall(r'jdbc.update\("([^"]+)"',text)
 db=work/'quota.db';c=sqlite3.connect(db);c.executescript((ROOT/'src/main/resources/db/migration/V5__login_throttle.sql').read_text());c.close()
 key=hashlib.sha256(b'admin@example.com').hexdigest();now=int(time.time()*1000)
 def attempt(i):
  c=sqlite3.connect(db,timeout=30,isolation_level=None)
  try:
   try:c.execute(insert,(key,now))
   except sqlite3.IntegrityError:pass
   c.execute(reset,(now,key,now-300000));return c.execute(admit,(key,)).rowcount==1
  finally:c.close()
 with concurrent.futures.ThreadPoolExecutor(max_workers=12) as pool:assert sum(pool.map(attempt,range(60)))==10
 print('PASS: SQL real em SQLite: 60 conexões/tentativas concorrentes, exatamente 10 aceitas.')
 print('LIMITAÇÃO: stubs e SQLite não substituem Spring, MySQL/H2, HTTP ou testes Maven.')
