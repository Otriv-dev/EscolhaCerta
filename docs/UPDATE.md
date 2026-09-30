# Aplicar o visual Moderna

1. Faça uma cópia da pasta atual como backup.
2. Extraia o ZIP e copie o conteúdo de `EscolhaCerta` sobre sua pasta atual. O pacote não contém `.env` nem uploads: mantenha esses arquivos locais.
3. Na pasta do projeto execute:

```powershell
docker compose up -d --build --force-recreate app
```

4. Acesse http://localhost:8080 e use Ctrl+F5 para atualizar o CSS.

Os nomes do projeto, serviços e volumes permanecem iguais. Não execute `down -v`. A migration V3 cadastra o WhatsApp da empresa (5511947067755) automaticamente na base existente. Não há mudança de entidade. O número continua editável pelo painel em Dados da empresa.

## Mudanças

- Paleta Moderna e hero centralizado conforme a prévia aprovada.
- Logo horizontal sem moldura, com proporções preservadas, no cabeçalho e rodapé.
- Botões arredondados, cartões mais leves e adaptação ao celular.
- MySQL sem publicação de porta no compose padrão; acesso opcional ao host por `docker-compose.local.yml`.

## Validação

Revisão de templates e parse das configurações; conferência da logo e integridade do pacote. A aplicação não foi executada neste ambiente por ausência de Maven, Docker e dependências. A execução local da versão anterior foi confirmada pelo usuário. Esta atualização precisa ser conferida após o rebuild no seu computador.

## WhatsApp

Botão fixo no canto inferior direito das páginas públicas, com mensagem pronta de solicitação de orçamento. Abre o WhatsApp em nova aba, sem enviar a mensagem automaticamente. Número cadastrado: (11) 94706-7755. A atualização V3 ocorre ao iniciar a aplicação. Caso altere o número depois pelo CMS, ele será respeitado nos próximos acessos.

## Remoção de cuidado com crianças

A migration V4 remove o serviço inicial “Cuidado com crianças” em bases novas e existentes. A V2 permanece intacta para preservar os checksums de migrations já executadas. O serviço deixa de aparecer na página inicial, na listagem de serviços e no CMS após reiniciar a aplicação atualizada.

## Credenciais administrativas

O e-mail inicial padrão é `admin@escolhacerta.local`. A senha foi gerada pelo script e está em `ADMIN_PASSWORD` no seu `.env`. Se alterou a senha no painel, utilize a senha nova; mudar o `.env` não redefine uma conta existente.

## Revisão de segurança

Incluídas correções de sessões após troca de senha, limites compartilhados de envio e limites de upload/recursos. Consulte SECURITY_REVIEW.md para achados e verificações pendentes. Esta atualização não redefine a senha do administrador.
