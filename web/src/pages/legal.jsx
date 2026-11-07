import { Link } from 'react-router-dom'
import { LEGAL } from '../lib/legal'
import { Pagina, estilos } from '../components/ui'

const Email = () => <a href={`mailto:${LEGAL.email}`} className={estilos.link}>{LEGAL.email}</a>

function Documento({ titulo, children }) {
  return (
    <Pagina titulo={titulo} largura="max-w-3xl">
      <p className="mb-6 text-sm text-muted">Atualizado em {LEGAL.updatedAt}.</p>
      <div className="space-y-4 leading-relaxed [&_h2]:mt-8 [&_h2]:text-xl [&_h2]:font-bold [&_li]:ml-5 [&_li]:list-disc [&_td]:border [&_td]:border-border [&_td]:p-2 [&_th]:border [&_th]:border-border [&_th]:bg-surface-2 [&_th]:p-2 [&_th]:text-left">
        {children}
      </div>
    </Pagina>
  )
}

function Tabela({ cabecalho, linhas }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-sm">
        <thead><tr>{cabecalho.map((c) => <th key={c} scope="col">{c}</th>)}</tr></thead>
        <tbody>{linhas.map((l) => <tr key={l[0]}>{l.map((c, i) => <td key={i}>{c}</td>)}</tr>)}</tbody>
      </table>
    </div>
  )
}

export function PrivacidadePage() {
  return (
    <Documento titulo="Política de privacidade">
      <p>
        Esta Política explica como o {LEGAL.appName} (site {LEGAL.site} e aplicativo Android) trata dados pessoais,
        conforme a Lei Geral de Proteção de Dados Pessoais (Lei nº 13.709/2018, "LGPD").
      </p>

      <h2>1. Quem é o responsável</h2>
      <p>
        O controlador é {LEGAL.controller} ({LEGAL.controllerType}), {LEGAL.forum}. Canal para qualquer assunto sobre os
        seus dados: <Email />. Como agente de tratamento de pequeno porte, não indicamos encarregado (Resolução CD/ANPD nº
        2/2022); o canal é respondido diretamente pelo responsável.
      </p>

      <h2>2. Dados que tratamos, para quê e com qual base legal</h2>
      <Tabela cabecalho={['Dado', 'Para quê', 'Base legal (LGPD)']} linhas={[
        ['Seu nome, e-mail e senha (guardada só como hash)', 'Criar a conta, entrar e avisar você quando alguém encontrar um objeto', 'Execução de contrato (art. 7º, V)'],
        ['Objetos que você cadastra (nome, categoria, recado público e anotações privadas)', 'Mostrar o objeto a quem ler o QR Code e organizar seus pertences', 'Execução de contrato (art. 7º, V)'],
        ['Etiquetas (código do QR Code, guardado como hash e cifrado)', 'Ligar o QR Code ao objeto e deixar você baixar o mesmo QR Code de novo', 'Execução de contrato (art. 7º, V)'],
        ['Avisos de quem encontrou: a mensagem e, se a pessoa quiser, um contato', 'Entregar a mensagem ao dono do objeto para combinar a devolução', 'Consentimento de quem envia (art. 7º, I)'],
        ['Data e versão do aceite destes documentos', 'Comprovar o aceite', 'Exercício regular de direitos (art. 7º, VI)'],
        ['Registros de erro (sem nome, e-mail, IP nem conteúdo)', 'Corrigir falhas e lentidão', 'Legítimo interesse (art. 7º, IX)'],
        ['Endereço IP, data e hora', 'Limitar tentativas e bloquear abusos; o IP fica só na memória do servidor', 'Legítimo interesse (art. 7º, IX)'],
      ]} />
      <p>
        <strong>Quem encontra um objeto vê só</strong> o nome do objeto, a categoria e o recado que o dono escreveu. Nunca vê
        o nome, o e-mail ou qualquer outro dado do dono. O dono só vê o contato de quem encontrou se a pessoa decidir
        deixar. Não usamos localização, contatos do celular, fotos, anúncios nem ferramentas de rastreamento. No navegador,
        guardamos só o necessário para manter você conectado.
      </p>

      <h2>3. Com quem compartilhamos</h2>
      <Tabela cabecalho={['Fornecedor', 'Para quê', 'Onde']} linhas={[
        ['Render', 'Servidor da API', 'Estados Unidos'],
        ['Neon', 'Banco de dados', 'Estados Unidos (Virgínia), na mesma região do servidor'],
        ['Vercel', 'Hospedagem do site', 'Estados Unidos e rede global'],
        ['Resend', 'Envio dos e-mails (códigos e avisos)', 'São Paulo, Brasil'],
        ['Sentry', 'Registro de erros, sem dados pessoais', 'Estados Unidos'],
        ['GitHub', 'Cópia de segurança diária do banco, criptografada', 'Estados Unidos'],
        ['ImprovMX e Google (Gmail)', 'Recebimento das mensagens enviadas ao nosso e-mail de contato', 'Estados Unidos'],
        ['Google Play', 'Distribuição do app; segue a política de privacidade do Google', 'Estados Unidos'],
      ]} />
      <p>
        Também podemos compartilhar dados quando exigido por lei ou ordem de autoridade competente. Não vendemos dados
        pessoais. Parte dos fornecedores fica fora do Brasil; essas transferências acontecem só para o serviço funcionar,
        com fornecedores que adotam medidas de segurança adequadas (art. 33 da LGPD).
      </p>

      <h2>4. Por quanto tempo guardamos</h2>
      <Tabela cabecalho={['Dado', 'Prazo']} linhas={[
        ['Conta, objetos e etiquetas', 'Enquanto a conta existir. Ao excluir a conta, tudo é apagado na hora'],
        ['Cadastros nunca confirmados e sem objetos', '30 dias'],
        ['Avisos de quem encontrou (mensagem e contato)', '90 dias, ou antes, se o dono apagar'],
        ['Etiquetas desativadas', '30 dias depois de desativadas'],
        ['Códigos enviados por e-mail e sessões', 'Até vencer, mais 1 dia'],
        ['Cópias de segurança criptografadas', '30 dias'],
        ['Registros de erro', 'Prazo do plano do Sentry, até 90 dias'],
      ]} />

      <h2 id="direitos">5. Seus direitos</h2>
      <p>
        Pela LGPD (art. 18), você pode pedir confirmação e acesso, correção, anonimização, bloqueio ou eliminação,
        portabilidade, informação sobre compartilhamento e revogação do consentimento. Em <strong>Conta</strong>, você
        corrige o nome, baixa uma cópia completa dos seus dados (<strong>Baixar meus dados</strong>) e exclui a conta. Quem
        enviou um aviso pode pedir a exclusão da mensagem e do contato por <Email />. Respondemos em até 15 dias. Você também
        pode reclamar à Autoridade Nacional de Proteção de Dados (ANPD).
      </p>

      <h2>6. Cookies e armazenamento no navegador</h2>
      <p>
        Não usamos cookies de publicidade, de análise nem de terceiros. O site guarda no seu navegador (armazenamento
        local) só o que é necessário para funcionar: as chaves da sua sessão, para manter você conectado.
        Se você escolher o tema claro no rodapé, guardamos também essa preferência. Esses dados ficam só no seu aparelho e são apagados quando você sai da conta (a sessão) ou limpa os dados do navegador. Por
        serem estritamente necessários ao serviço que você pediu, não dependem de consentimento. As fontes do site são
        servidas pelo próprio site, sem pedidos a outras empresas.
      </p>

      <h2>7. Segurança</h2>
      <p>
        HTTPS sempre; senhas com bcrypt; QR Codes com código aleatório de 256 bits, guardado só como hash e cifrado com
        AES-256-GCM; códigos por e-mail com validade curta e poucas tentativas; bloqueio após senhas erradas; limites de
        pedidos por IP; e cópia de segurança diária criptografada. Se acontecer um incidente que possa causar risco
        relevante, avisaremos você e a ANPD.
      </p>

      <h2>8. Idade mínima e mudanças</h2>
      <p>
        O {LEGAL.appName} é destinado a maiores de 18 anos. Quando esta Política mudar, atualizaremos a data no topo e
        avisaremos no site, no app ou por e-mail se a mudança for importante.
      </p>
    </Documento>
  )
}

export function TermosPage() {
  return (
    <Documento titulo="Termos de uso">
      <p>
        Estes Termos regulam o uso do {LEGAL.appName} (site {LEGAL.site}, aplicativo Android e API), mantido por{' '}
        {LEGAL.controller} ({LEGAL.controllerType}), {LEGAL.forum}. Ao criar uma conta, você declara que leu e aceita
        estes Termos e que está ciente da <Link to="/privacidade" className={estilos.link}>Política de privacidade</Link>.
      </p>

      <h2>1. O que é o Valtrea Volta</h2>
      <p>
        Um serviço para identificar objetos com etiquetas de QR Code. Quem encontra um objeto lê o QR Code com a câmera do
        celular e avisa o dono, sem que um veja os dados do outro. O serviço está em fase de testes e é gratuito.
      </p>
      <p>
        <strong>O Valtrea Volta não garante que um objeto perdido será encontrado ou devolvido.</strong> Ele só facilita o
        contato. A devolução é combinada diretamente entre as pessoas, que são responsáveis por ela. Não guardamos,
        transportamos nem entregamos objetos.
      </p>

      <h2>2. Conta</h2>
      <ul>
        <li>É preciso ter 18 anos ou mais e informar dados verdadeiros.</li>
        <li>Você é responsável pela sua senha e pelo que for feito na sua conta.</li>
        <li>Para gerar etiquetas é preciso confirmar o e-mail, porque é por ele que avisamos quando alguém encontrar.</li>
      </ul>

      <h2>3. Uso permitido</h2>
      <p>Você concorda em não:</p>
      <ul>
        <li>colocar etiquetas em objetos que não são seus ou usar o serviço para rastrear, assediar ou enganar pessoas;</li>
        <li>escrever no recado público ou nos avisos conteúdo ofensivo, discriminatório, ameaçador, propaganda ou dados pessoais de outras pessoas;</li>
        <li>pedir ou oferecer pagamento para devolver um objeto encontrado;</li>
        <li>tentar acessar dados que não são seus, explorar falhas, sobrecarregar o serviço ou enviar avisos automatizados.</li>
      </ul>
      <p>
        Podemos suspender ou encerrar contas que violem estes Termos, avisando o motivo pelo e-mail da conta, exceto
        quando houver risco a outras pessoas. Para denunciar um uso indevido, escreva para <Email />.
      </p>

      <h2>4. Dicas de segurança na devolução</h2>
      <p>
        Combine a entrega em lugar público e movimentado, de dia, ou use um balcão de achados e perdidos. Nunca pague para
        receber um objeto de volta e desconfie de quem pedir dinheiro ou dados bancários.
      </p>

      <h2>5. Disponibilidade e mudanças</h2>
      <p>
        Por ser uma versão de testes em hospedagem gratuita, o serviço pode demorar a responder ou ficar fora do ar, e
        funções podem mudar. Mudanças relevantes serão avisadas no site, no app ou por e-mail.
      </p>

      <h2>6. Responsabilidades</h2>
      <p>
        O conteúdo dos recados e avisos é de responsabilidade de quem escreveu. Nada nestes Termos limita os direitos que
        você tem como consumidor pela legislação brasileira.
      </p>

      <h2>7. Propriedade intelectual</h2>
      <p>
        A marca Valtrea, o nome {LEGAL.appName}, o visual e o código do serviço pertencem ao responsável e não podem ser
        copiados nem usados sem autorização. O que você escreve (os recados e os nomes dos objetos) continua sendo seu: você só nos autoriza
        a guardar e mostrar esse conteúdo no serviço enquanto ele estiver publicado, e declara que pode publicá-lo.
      </p>

      <h2>8. Encerramento, alterações e foro</h2>
      <p>
        Você pode excluir a conta a qualquer momento em <strong>Conta → Excluir conta</strong>. Se não concordar com uma
        nova versão destes Termos, pode excluir a conta sem custo. Estes Termos seguem as leis do Brasil; fica eleito o foro
        de {LEGAL.forum}, ressalvado o direito do consumidor de propor ação no foro do seu domicílio. Contato: <Email />.
      </p>
    </Documento>
  )
}

export function AcessibilidadePage() {
  return (
    <Documento titulo="Declaração de acessibilidade">
      <p>
        Queremos que o {LEGAL.appName} possa ser usado por todas as pessoas, incluindo pessoas com deficiência, como prevê a
        Lei Brasileira de Inclusão (Lei nº 13.146/2015, art. 63). Buscamos atender às WCAG 2.2, nível AA, e à ABNT NBR 17225.
      </p>
      <h2>O que já fizemos</h2>
      <ul>
        <li>As principais páginas são verificadas automaticamente (axe-core, WCAG A e AA) a cada mudança, no computador e no celular.</li>
        <li>Navegação completa pelo teclado, foco visível e link “Pular para o conteúdo”.</li>
        <li>Campos com rótulo visível e mensagens de erro em texto, ligadas ao campo.</li>
        <li>Contraste revisado no tema escuro; o zoom do navegador e do celular funciona em todas as páginas.</li>
        <li>No app Android, telas com os componentes do Material 3, que funcionam com o TalkBack.</li>
      </ul>
      <h2>Limitações conhecidas</h2>
      <ul>
        <li>Ler o QR Code depende da câmera de quem encontrou o objeto.</li>
        <li>Ainda não fizemos testes com pessoas que usam tecnologias assistivas, e não há tradução para Libras.</li>
      </ul>
      <h2>Encontrou uma barreira?</h2>
      <p>Escreva para <Email /> contando a página e o problema. Respondemos o quanto antes.</p>
    </Documento>
  )
}

export function ExcluirContaPage() {
  return (
    <Documento titulo="Como excluir a sua conta">
      <p>
        Entre no {LEGAL.appName} (site ou app) e vá em <strong>Conta → Excluir conta</strong>, confirmando com a sua senha.
        A conta, os objetos, as etiquetas e os avisos recebidos são apagados na hora, e os QR Codes param de funcionar.
      </p>
      <p>
        Sem acesso à conta? Use <Link to="/esqueci-senha" className={estilos.link}>Esqueci minha senha</Link> ou escreva
        para <Email /> com o assunto “Excluir conta”, a partir do e-mail cadastrado. Respondemos em até 15 dias.
      </p>
      <p>Cópias de segurança criptografadas são apagadas em até 30 dias, sem nenhum uso nesse período.</p>
    </Documento>
  )
}
