import os
import re

mapping = {
    'UserEntity': 'Utente',
    'TeamEntity': 'Team',
    'HackathonEntity': 'Hackathon',
    'TeamInviteEntity': 'Invito',
    'RegistrationEntity': 'Iscrizione',
    'SubmissionEntity': 'Sottomissione',
    'SupportRequestEntity': 'RichiestaSupporto',
    'EvaluationEntity': 'Valutazione',
    'ViolationReportEntity': 'SegnalazioneViolazione',
    'WinnerEntity': 'EsitoHackathon',
    'CallProposalEntity': 'CallSupporto',
    'UserRepository': 'UtenteRepository',
    'TeamInviteRepository': 'InvitoRepository',
    'RegistrationRepository': 'IscrizioneRepository',
    'SubmissionRepository': 'SottomissioneRepository',
    'SupportRequestRepository': 'RichiestaSupportoRepository',
    'EvaluationRepository': 'ValutazioneRepository',
    'ViolationReportRepository': 'SegnalazioneValidazioneRepository',
    'WinnerRepository': 'EsitoHackathonRepository',
    'CallProposalRepository': 'CallSupportoRepository',
}

pattern = re.compile('|'.join(r'\b' + re.escape(k) + r'\b' for k in sorted(mapping, key=len, reverse=True)))

def replace_in_file(path: str) -> bool:
    text = open(path, encoding='utf-8').read()
    new_text = pattern.sub(lambda m: mapping[m.group(0)], text)
    if new_text != text:
        open(path, 'w', encoding='utf-8').write(new_text)
        return True
    return False

root_dir = os.path.join('src', 'test', 'java')
for root, _, files in os.walk(root_dir):
    for f in files:
        if f.endswith('.java'):
            replace_in_file(os.path.join(root, f))
