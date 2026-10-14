# Kovick Mapper

Keymapper Android inspirado no fluxo de mapeamento de emuladores.

## O que já existe
- Projeto Android/Kotlin/Compose.
- Interface inicial.
- Perfis conceituais por jogo.
- Estrutura de mapeamentos.
- Serviço de acessibilidade declarado com suporte a gestos.
- Botão para abrir as configurações de acessibilidade.
- Base preparada para editor visual e modo mira.

## Próximas camadas
1. Editor visual sobre a tela.
2. Captura de teclado/mouse compatível com Android.
3. Modo mira com movimento relativo do mouse.
4. Perfis persistentes por pacote do jogo.
5. Ativação/desativação rápida.
6. Ajustes de sensibilidade e deadzone.
7. Exportação/importação de perfis.

## Limitação importante
O Android não permite que um aplicativo comum injete livremente qualquer entrada em outro aplicativo. O projeto usa APIs oficiais de acessibilidade/gestos e deve respeitar as permissões e limitações da versão do Android.