"""Valida fronteiras hexagonais de todos os módulos da API.

Uso: python scripts/check_architecture.py
As classes de domínio e casos de uso não podem importar framework, SQL ou
adaptadores de infraestrutura. O teste roda no GitHub Actions.
"""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1] / "backend/src/main/java/br/com/cursohub"
module_roots = [p for p in root.iterdir() if p.is_dir()]
sources = []
for module in module_roots:
    for layer in ("domain", "application"):
        directory = module / layer
        if directory.is_dir():
            sources.extend(directory.rglob("*.java"))

forbidden = (
    "org.springframework.",
    "jakarta.persistence.",
    "jakarta.servlet.",
    "jakarta.validation.",
    "com.nimbusds.",
    "org.postgresql.",
    "java.sql.",
)
adapter_pattern = re.compile(r"br\.com\.cursohub\..*\.(?:adapters|config)\.")

violations = []
for file in sorted(sources):
    for line_number, line in enumerate(file.read_text(encoding="utf-8").splitlines(), 1):
        stripped = line.strip()
        if not stripped.startswith("import "):
            continue
        imported = stripped.removeprefix("import ").removeprefix("static ").rstrip(";")
        if imported.startswith(forbidden) or adapter_pattern.match(imported):
            violations.append(f"{file.relative_to(root)}:{line_number}: {stripped}")

if not sources:
    print("ERRO: nenhuma classe de dominio ou aplicacao encontrada.")
    sys.exit(1)
if violations:
    print("VIOLAÇÕES DA ARQUITETURA HEXAGONAL:")
    print("\n".join(violations))
    sys.exit(1)

print(f"OK: {len(sources)} classes de dominio/aplicacao verificadas nos modulos.")
