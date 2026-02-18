import json

vpp = json.load(open('tmp_vpp_classes.json', encoding='utf-8'))
java = json.load(open('tmp_java_classes.json', encoding='utf-8'))

vpp_names = set(vpp.keys())
java_names = [item['name'] for item in java]

base_map = {
    'User': 'Utente',
    'Invite': 'Invito',
    'TeamInvite': 'Invito',
    'SupportRequest': 'RichiestaSupporto',
    'Support': 'Supporto',
    'Submission': 'Sottomissione',
    'Registration': 'Iscrizione',
    'Evaluation': 'Valutazione',
    'ViolationReport': 'SegnalazioneViolazione',
    'Violation': 'SegnalazioneViolazione',
    'CallProposal': 'CallSupporto',
    'Winner': 'EsitoHackathon',
    'Payment': 'Pagamento',
    'Authentication': 'Autenticazione',
}

suffixes = ('Entity', 'Controller', 'Service', 'Repository')

def expected_name(java_name: str) -> str:
    for sfx in suffixes:
        if java_name.endswith(sfx):
            base = java_name[:-len(sfx)]
            base = base_map.get(base, base)
            if sfx == 'Entity':
                return base
            return base + sfx
    return base_map.get(java_name, java_name)

report = []
mismatched = []
for name in java_names:
    exp = expected_name(name)
    if exp != name and exp in vpp_names:
        mismatched.append((name, exp))
    elif exp not in vpp_names and name in vpp_names:
        report.append((name, 'present-in-vpp-as-is'))

missing_in_java = []
for name in sorted(vpp_names):
    # only check for main layers
    if name.endswith(('Controller', 'Service', 'Repository')):
        if name not in java_names:
            missing_in_java.append(name)

print('MISMATCHED (java -> vpp expected):')
for a, b in mismatched:
    print(a, '->', b)

print('\nVPP layer classes missing in Java:')
for name in missing_in_java:
    print(name)
