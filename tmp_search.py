import pathlib
root=pathlib.Path('src')
needle='PagamentoPremio'
for p in root.rglob('*.java'):
    txt=p.read_text(encoding='utf-8', errors='ignore')
    if needle in txt:
        print(p)
