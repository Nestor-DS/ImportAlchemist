const input = document.querySelector('#jh');

const observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {

        if (mutation.type === 'attributes') {
            console.log(
                'Atributo modificado:',
                mutation.attributeName,
                'nuevo valor:',
                input.getAttribute(mutation.attributeName)
            );
        }

    });
});

observer.observe(input, {
    attributes: true,
    attributeOldValue: true
});