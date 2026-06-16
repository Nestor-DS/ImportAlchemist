const parent = document.querySelector('#mi-form'); // o document.body

const observer = new MutationObserver((mutations) => {
  mutations.forEach((mutation) => {

    // ✅ Cambió class, id, type, etc.
    if (mutation.type === 'attributes') {
      const el = mutation.target;
      console.log(`Atributo "${mutation.attributeName}" cambió en:`, el);
      console.log('Antes:', mutation.oldValue);
      console.log('Ahora:', el.getAttribute(mutation.attributeName));
    }

    // ✅ Se eliminó un input
    mutation.removedNodes.forEach((node) => {
      if (node.tagName === 'INPUT') {
        console.log('Input eliminado:', node);
      }
    });

    // ✅ Se añadió un input
    mutation.addedNodes.forEach((node) => {
      if (node.tagName === 'INPUT') {
        console.log('Input añadido:', node);
      }
    });

  });
});

observer.observe(parent, {
  attributes: true,        // Para detectar cambios de atributos
  attributeOldValue: true, // Guarda el valor anterior
  childList: true,         // Para detectar nodos añadidos/eliminados
  subtree: true            // Aplica a todos los descendientes del padre
});