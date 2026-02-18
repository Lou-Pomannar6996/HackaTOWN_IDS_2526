import sys

path = sys.argv[1]
mapping = {
    'userRepository': 'utenteRepository',
    'registrationRepository': 'iscrizioneRepository',
    'submissionRepository': 'sottomissioneRepository',
    'evaluationRepository': 'valutazioneRepository',
    'winnerRepository': 'esitoHackathonRepository',
    'supportRequestRepository': 'richiestaSupportoRepository',
    'callProposalRepository': 'callSupportoRepository',
    'violationReportRepository': 'segnalazioneValidazioneRepository',
}

text = open(path, encoding='utf-8').read()
for old, new in mapping.items():
    text = text.replace(old, new)
open(path, 'w', encoding='utf-8').write(text)
