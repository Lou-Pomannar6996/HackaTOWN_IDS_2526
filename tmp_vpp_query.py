import json, sys 
data=json.load(open('tmp_vpp_classes.json', encoding='utf-8')) 
name=sys.argv[1] if len(sys.argv) > 1 else '' 
print(json.dumps(data.get(name), ensure_ascii=False, indent=2)) 
