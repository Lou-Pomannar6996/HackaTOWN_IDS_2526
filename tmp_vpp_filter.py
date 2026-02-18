import json, sys 
data=json.load(open('tmp_vpp_classes.json', encoding='utf-8')) 
substr = sys.argv[1] if len(sys.argv) > 1 else '' 
for name in sorted(data.keys()): 
    if substr in name: 
        print(name) 
