import json

def get_classes(q_id_str):
    q_id = int(q_id_str)
    classes = ["B2", "C", "D", "E", "F"]
    
    # B1 exclude 167-192
    if q_id not in range(167, 193):
        classes.append("B1")
        
    # A1 heuristic
    is_a1 = (1 <= q_id <= 80) or (193 <= q_id <= 213) or (305 <= q_id <= 354) or (487 <= q_id <= 535)
    if is_a1:
        classes.append("A1")
        
    # A2 heuristic
    is_a2 = (1 <= q_id <= 166) or (193 <= q_id <= 269) or (305 <= q_id <= 411) or (487 <= q_id <= 586)
    if is_a2:
        classes.append("A2")
        
    return classes

with open('./composeApp/src/commonMain/composeResources/files/questions.json', 'r') as f:
    questions = json.load(f)

for q in questions:
    q['licenseClasses'] = get_classes(q['id'])

with open('./composeApp/src/commonMain/composeResources/files/questions.json', 'w') as f:
    json.dump(questions, f, ensure_ascii=False, indent=2)
