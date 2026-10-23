"""Conferência simples das fronteiras da arquitetura. Execute na raiz do projeto."""
from pathlib import Path
import sys

base = Path(__file__).resolve().parents[1] / 'backend/src/main/java/br/com/cursohub/catalog'
forbidden = (
    'import org.springframework.', 'import jakarta.persistence.',
    'import br.com.cursohub.catalog.adapters.',
    'import br.com.cursohub.catalog.config.',
)
files = list((base / 'domain').rglob('*.java')) + list((base / 'application').rglob('*.java'))
violations = []
for file in files:
    for number, line in enumerate(file.read_text(encoding='utf-8').splitlines(), 1):
        if line.strip().startswith(forbidden):
            violations.append(f'{file.relative_to(base)}:{number}: {line.strip()}')
if violations:
    print('VIOLAÇÕES DAS FRONTEIRAS:')
    print('\n'.join(violations))
    sys.exit(1)
print(f'OK: {len(files)} arquivos de domínio/aplicação sem imports de infraestrutura/framework.')
