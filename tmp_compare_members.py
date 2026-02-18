import os
import re
import json

vpp = json.load(open('tmp_vpp_classes.json', encoding='utf-8'))

field_pat = re.compile(r'^\s*(private|protected|public)\s+(?:static\s+)?(?:final\s+)?[\w<>\[\], ?]+\s+(\w+)\s*(=|;)', re.MULTILINE)
method_pat = re.compile(r'^\s*(public|protected|private)\s+(?:static\s+)?[\w<>\[\], ?]+\s+(\w+)\s*\(', re.MULTILINE)

def parse_java(path: str):
    text = open(path, encoding='utf-8').read()
    m = re.search(r'\b(class|interface|enum)\s+(\w+)', text)
    if not m:
        return None
    name = m.group(2)
    fields = [f for _, f, _ in field_pat.findall(text)]
    methods = [m for _, m in method_pat.findall(text) if m != name]
    return name, set(fields), set(methods)

java_index = {}
for root, _, files in os.walk(os.path.join('src', 'main', 'java')):
    for f in files:
        if f.endswith('.java'):
            parsed = parse_java(os.path.join(root, f))
            if parsed:
                name, fields, methods = parsed
                java_index[name] = {'fields': fields, 'methods': methods}

def report_for(name: str):
    v = vpp.get(name)
    j = java_index.get(name)
    if not v or not j:
        return None
    v_attrs = set(v.get('attributes', []))
    v_ops = set(v.get('operations', []))
    j_fields = j['fields']
    j_methods = j['methods']
    missing_fields = sorted(v_attrs - j_fields)
    extra_fields = sorted(j_fields - v_attrs)
    missing_methods = sorted(v_ops - j_methods)
    return {
        'class': name,
        'missing_fields': missing_fields,
        'extra_fields': extra_fields,
        'missing_methods': missing_methods,
    }

classes_to_check = [
    'Utente', 'Team', 'Hackathon', 'Invito', 'Iscrizione', 'RichiestaSupporto',
    'Sottomissione', 'Valutazione', 'SegnalazioneViolazione', 'CallSupporto', 'EsitoHackathon',
    'AutenticazioneController', 'UtenteController', 'SupportoController', 'TeamController', 'InvitoController',
    'AutenticazioneService', 'UtenteService', 'SupportoService', 'TeamService'
]

for cls in classes_to_check:
    rep = report_for(cls)
    if rep:
        print(json.dumps(rep, ensure_ascii=False))
