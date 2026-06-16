const parent = document.querySelector('#mi-form'); // o document.body

const observer = new MutationObserver((mutations) => {
 mutations.forEach((mutation) => {

   // Cambió un atributo (class, id, type, etc.)
   if (mutation.type === 'attributes') {
     const tenia = mutation.oldValue?.includes('required');
     const tiene = mutation.target.classList.contains('required');

     if (tenia || tiene) {
       location.reload();
     }
   }

   // Se eliminó un input.required
   mutation.removedNodes.forEach((node) => {
     if (node.tagName === 'INPUT' && node.classList.contains('required')) {
       location.reload();
     }
   });

   // Se añadió un input.required
   mutation.addedNodes.forEach((node) => {
     if (node.tagName === 'INPUT' && node.classList.contains('required')) {
       location.reload();
     }
   });

 });
});

observer.observe(parent, {
 attributes: true,
 attributeOldValue: true,
 childList: true,
 subtree: true
});