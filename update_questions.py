import json

def get_classes(q_id_str):
    q_id = int(q_id_str)
    classes = ["B2", "C", "D", "E", "F"]
    
    # B1 exclude 167-192
    if q_id not in range(167, 193):
        classes.append("B1")
        
    return classes

with open('./composeApp/src/commonMain/composeResources/files/questions.json', 'r') as f:
    questions = json.load(f)

for q in questions:
    q['licenseClasses'] = get_classes(q['id'])

with open('./composeApp/src/commonMain/composeResources/files/questions.json', 'w') as f:
    json.dump(questions, f, ensure_ascii=False, indent=2)
