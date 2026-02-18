import sqlite3, re 
conn=sqlite3.connect('VPP/Ingegneria_del_software_25_26.vpp') 
cur=conn.cursor() 
cur.execute('SELECT NAME, DEFINITION FROM MODEL_ELEMENT WHERE MODEL_TYPE=?', ('Class',)) 
rows=cur.fetchall() 
print('classes', len(rows)) 
name, definition = rows[0] 
text = definition.decode('utf-8', 'ignore') if definition else '' 
print('name', name) 
print(text[:800]) 
