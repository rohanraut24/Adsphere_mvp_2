import os
import re

src_dir = r"c:\Users\Admin\Adsphere\frontend-2\src\app"

def migrate_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # Replace imports
    content = re.sub(
        r"import\s+{[^}]*\bLucideComponent\b[^}]*}\s+from\s+['\"]@lucide/angular['\"];",
        lambda m: m.group(0).replace("LucideComponent", "LucideDynamicIcon"),
        content
    )
    
    # Replace LucideComponent inside imports: [ ... ]
    # Look for imports array: imports: [ ... ]
    # We do a replacement of LucideComponent inside imports: [ ... ]
    def replace_imports_array(match):
        imports_block = match.group(0)
        return imports_block.replace("LucideComponent", "LucideDynamicIcon")
        
    content = re.sub(r"imports\s*:\s*\[[^\]]*\]", replace_imports_array, content)
    
    # Replace templates
    # Replace static name: <lucide-icon name="x" -> <svg lucideIcon="x"
    # Replace dynamic name: <lucide-icon [name]="x" -> <svg [lucideIcon]="x"
    content = re.sub(r"<lucide-icon\s+name=\"([^\"]+)\"", r'<svg lucideIcon="\1"', content)
    content = re.sub(r"<lucide-icon\s+\[name\]=\"([^\"]+)\"", r'<svg [lucideIcon]="\1"', content)
    
    # Replace other attributes on <lucide-icon e.g. <lucide-icon *ngIf="..." name="..." -> <svg *ngIf="..." lucideIcon="..."
    # To be very generic, we can replace any <lucide-icon with <svg and any name="..." or [name]="..." with lucideIcon or [lucideIcon]
    # But wait, does lucide-icon have any other tags? Let's check:
    # We can match <lucide-icon and replace it with <svg, and then inside that tag, replace name="..." with lucideIcon="..." and [name] with [lucideIcon]
    def replace_lucide_tag(match):
        tag_content = match.group(0)
        # replace tag name
        tag_content = tag_content.replace("<lucide-icon", "<svg")
        # replace name=
        tag_content = re.sub(r"\bname=\"([^\"]+)\"", r'lucideIcon="\1"', tag_content)
        tag_content = re.sub(r"\b\[name\]=\"([^\"]+)\"", r'[lucideIcon]="\1"', tag_content)
        return tag_content

    content = re.sub(r"<lucide-icon\b[^>]*>", replace_lucide_tag, content)
    
    # Replace closing tags
    content = content.replace("</lucide-icon>", "</svg>")

    # Specially fix StatCardComponent if we are in it
    if "stat-card.component.ts" in filepath:
        if "Math = Math;" not in content:
            # Inject protected readonly Math = Math; after class declaration
            content = re.sub(
                r"export class StatCardComponent {",
                "export class StatCardComponent {\n  protected readonly Math = Math;",
                content
            )

    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Migrated: {filepath}")

# Walk through all directories
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith(('.ts', '.html')):
            migrate_file(os.path.join(root, file))
