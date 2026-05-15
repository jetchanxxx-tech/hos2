#!/usr/bin/env python3
"""Remove Lombok annotations and generate manual getters/setters/builders."""
import re, os

ENTITY_DIR = "backend/src/main/java/com/huifu/starchain/entity"
COMMON_DIR = "backend/src/main/java/com/huifu/starchain/common"

def extract_fields(content):
    """Extract field names and types from Java class body."""
    fields = []
    # Match: @Column(...) \n private Type name;
    pattern = r'private\s+(\w+(?:<[\w\s,.]+>)?)\s+(\w+)\s*;'
    for m in re.finditer(pattern, content):
        fields.append((m.group(1), m.group(2)))
    return fields

def generate_getters_setters(fields):
    lines = []
    for ftype, fname in fields:
        cap_name = fname[0].upper() + fname[1:]
        # getter
        lines.append(f"    public {ftype} get{cap_name}() {{ return {fname}; }}")
        # setter
        lines.append(f"    public void set{cap_name}({ftype} {fname}) {{ this.{fname} = {fname}; }}")
    return lines

def process_entity(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Skip files without Lombok
    if '@Getter' not in content and '@Builder' not in content:
        return

    # Remove Lombok imports
    content = re.sub(r'import lombok\.\*;\n', '', content)
    content = re.sub(r'import lombok\.(Getter|Setter|Builder|NoArgsConstructor|AllArgsConstructor);\n', '', content)
    content = re.sub(r'import lombok\.(Getter|Setter|Builder|NoArgsConstructor|AllArgsConstructor);\r?\n', '', content)

    # Remove Lombok annotations on the class
    content = re.sub(r'@Getter\s*\n', '', content)
    content = re.sub(r'@Setter\s*\n', '', content)
    content = re.sub(r'@Builder\s*\n', '', content)
    content = re.sub(r'@NoArgsConstructor\s*\n', '', content)
    content = re.sub(r'@AllArgsConstructor\s*\n', '', content)
    content = re.sub(r'@RequiredArgsConstructor\s*\n', '', content)

    # Extract fields
    fields = extract_fields(content)

    # Remove @Builder.Default
    content = re.sub(r'@Builder\.Default\s*\n', '', content)

    # Find the class closing brace (last line)
    # Insert getters/setters before the last }
    getset = generate_getters_setters(fields)

    # Add no-args constructor after fields, before getters
    # Find the last field declaration
    last_field_end = 0
    for m in re.finditer(r'private\s+(\w+(?:<[\w\s,.]+>)?)\s+(\w+)\s*;', content):
        last_field_end = m.end()

    if last_field_end > 0:
        # Insert after last field
        insert_pos = content.index('\n', last_field_end) + 1
        class_name = os.path.basename(filepath).replace('.java', '')

        # Constructor
        constructor = f"""
    public {class_name}() {{}}

    // ---- Getters & Setters ----
"""
        getset_text = '\n'.join(getset)

        content = content[:insert_pos] + constructor + getset_text + '\n' + content[insert_pos:]

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"  Processed: {os.path.basename(filepath)} ({len(fields)} fields)")

# Process all entity files
for f in sorted(os.listdir(ENTITY_DIR)):
    if f.endswith('.java'):
        process_entity(os.path.join(ENTITY_DIR, f))

# Also process common response classes
for root, dirs, files in os.walk(COMMON_DIR):
    for f in files:
        if f.endswith('.java'):
            process_entity(os.path.join(root, f))
