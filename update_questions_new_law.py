import json

def get_classes(q_id_str):
    q_id = int(q_id_str)
    # Start with base car classes for all 600 questions
    classes = ["B", "C1", "C"]
    
    # B1 exclude 167-192 (Nghiệp vụ vận tải)
    if q_id not in range(167, 193):
        classes.append("B1")
        
    return classes

with open('./composeApp/src/commonMain/composeResources/files/questions.json', 'r', encoding='utf-8') as f:
    questions = json.load(f)

for q in questions:
    q['licenseClasses'] = get_classes(q['id'])

with open('./composeApp/src/commonMain/composeResources/files/questions.json', 'w', encoding='utf-8') as f:
    json.dump(questions, f, ensure_ascii=False, indent=2)
