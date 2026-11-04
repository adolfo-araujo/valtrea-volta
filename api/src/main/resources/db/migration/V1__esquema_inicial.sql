-- Valtrea Volta: donos, objetos, etiquetas com QR Code e avisos de quem encontrou.
-- Excluir uma conta apaga tudo dela em cascata (LGPD: exclusão na hora).

CREATE TABLE usuarios (
    id                        UUID PRIMARY KEY,
    nome                      VARCHAR(120) NOT NULL,
    email                     VARCHAR(254) NOT NULL,
    senha_hash                VARCHAR(100) NOT NULL,
    papel                     VARCHAR(20)  NOT NULL,
    tentativas_login_falhas   INTEGER      NOT NULL DEFAULT 0,
    bloqueado_ate             TIMESTAMPTZ,
    email_confirmado_em       TIMESTAMPTZ,
    criado_em                 TIMESTAMPTZ  NOT NULL,
    termos_versao             VARCHAR(20),
    termos_aceitos_em         TIMESTAMPTZ,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

-- Sessões do app e do site. Só o hash do token: quem ler o banco não consegue usá-lo.
CREATE TABLE sessoes (
    id              UUID PRIMARY KEY,
    usuario_id      UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) NOT NULL,
    expira_em       TIMESTAMPTZ NOT NULL,
    criado_em       TIMESTAMPTZ NOT NULL,
    revogado_em     TIMESTAMPTZ,
    CONSTRAINT uk_sessoes_token UNIQUE (token_hash)
);
CREATE INDEX ix_sessoes_usuario ON sessoes (usuario_id);

-- Códigos de 6 dígitos enviados por e-mail (confirmação de e-mail e nova senha), só como HMAC.
CREATE TABLE codigos_email (
    id              UUID PRIMARY KEY,
    usuario_id      UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    finalidade      VARCHAR(20) NOT NULL,
    codigo_hash     VARCHAR(64) NOT NULL,
    tentativas      INTEGER     NOT NULL DEFAULT 0,
    expira_em       TIMESTAMPTZ NOT NULL,
    criado_em       TIMESTAMPTZ NOT NULL,
    usado_em        TIMESTAMPTZ
);
CREATE INDEX ix_codigos_email_usuario ON codigos_email (usuario_id, finalidade, criado_em DESC);

CREATE TABLE objetos (
    id                UUID PRIMARY KEY,
    dono_id           UUID         NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    nome              VARCHAR(80)  NOT NULL,
    categoria         VARCHAR(30)  NOT NULL,
    descricao         VARCHAR(500),
    mensagem_publica  VARCHAR(200),
    perdido           BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em         TIMESTAMPTZ  NOT NULL,
    atualizado_em     TIMESTAMPTZ  NOT NULL
);
CREATE INDEX ix_objetos_dono ON objetos (dono_id, criado_em DESC);

-- O QR Code leva só um código aleatório de 256 bits. Aqui fica o hash (para achar a etiqueta) e uma cópia cifrada
-- com AES-256-GCM (para o dono baixar o mesmo QR Code de novo).
CREATE TABLE etiquetas (
    id              UUID PRIMARY KEY,
    objeto_id       UUID        NOT NULL REFERENCES objetos (id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) NOT NULL,
    token_cifrado   TEXT        NOT NULL,
    criada_em       TIMESTAMPTZ NOT NULL,
    desativada_em   TIMESTAMPTZ,
    CONSTRAINT uk_etiquetas_token UNIQUE (token_hash)
);
CREATE INDEX ix_etiquetas_objeto ON etiquetas (objeto_id);

-- "Encontrei seu objeto": mensagem e, se a pessoa quiser, um contato. Apagados 90 dias depois.
CREATE TABLE avisos (
    id              UUID PRIMARY KEY,
    objeto_id       UUID          NOT NULL REFERENCES objetos (id) ON DELETE CASCADE,
    mensagem        VARCHAR(1000) NOT NULL,
    contato         VARCHAR(120),
    status          VARCHAR(20)   NOT NULL,
    criado_em       TIMESTAMPTZ   NOT NULL,
    atualizado_em   TIMESTAMPTZ   NOT NULL
);
CREATE INDEX ix_avisos_objeto ON avisos (objeto_id, criado_em DESC);
