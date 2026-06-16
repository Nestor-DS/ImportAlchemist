<script>
  // ─── Configuración ────────────────────────────────────────────────
  const REQUIRED_FIELDS = [
    { selector: '#nombre', type: 'input' },
    { selector: '#email',  type: 'input' },
    { selector: '#pais',   type: 'select' },
  ];

  // ─── 1. Fingerprints ──────────────────────────────────────────────
  const fieldFingerprints = new Map();

  function captureFingerprints() {
    REQUIRED_FIELDS.forEach(({ selector }) => {
      const el = document.querySelector(selector);
      if (el) {
        fieldFingerprints.set(selector, {
          id:       el.id,
          name:     el.name,
          tagName:  el.tagName,
          required: el.hasAttribute('required'),
        });
      }
    });
  }

  // ─── 2. Integridad ────────────────────────────────────────────────
  function verifyIntegrity() {
    const errors = [];

    for (const [selector, original] of fieldFingerprints) {
      const el = document.querySelector(selector);

      if (!el) {
        errors.push(`Campo eliminado del DOM: ${selector}`);
        continue;
      }
      if (el.id !== original.id)
        errors.push(`ID alterado en: ${selector}`);
      if (el.name !== original.name)
        errors.push(`name alterado en: ${selector}`);
      if (!el.hasAttribute('required'))
        errors.push(`Atributo required removido en: ${selector}`);
    }

    return errors;
  }

  // ─── 3. Valores ───────────────────────────────────────────────────
  function validateValues() {
    const errors = [];

    REQUIRED_FIELDS.forEach(({ selector }) => {
      const el = document.querySelector(selector);
      if (!el) return;

      const value = el.value?.trim();
      if (!value || value === '' || value === 'null' || value === 'undefined') {
        errors.push(`Campo vacío: ${selector}`);
      }
    });

    return errors;
  }

  // ─── Submit ───────────────────────────────────────────────────────
  document.querySelector('#miFormulario').addEventListener('submit', (e) => {
    e.preventDefault();

    const allErrors = [...verifyIntegrity(), ...validateValues()];

    if (allErrors.length > 0) {
      console.warn('Validación fallida:', allErrors);
      alert('Errores de validación:\n' + allErrors.join('\n'));
      return;
    }

    console.log('Formulario válido ✓');
    // submitForm();
  });

  // ─── Init ─────────────────────────────────────────────────────────
  document.addEventListener('DOMContentLoaded', captureFingerprints);
</script>