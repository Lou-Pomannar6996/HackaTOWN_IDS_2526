import json 
data=json.load(open('tmp_vpp_classes.json', encoding='utf-8')) 
suffixes = ('Controller','Service','Repository','Strategy','Factory','State','Gateway','Mapper','Request','Response','DTO') 
for name in sorted(data.keys()): 
    if not name.endswith(suffixes): 
        print(name) 
