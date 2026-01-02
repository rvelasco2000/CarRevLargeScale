/**
 * This code has been generated via gemini and allow us to automatically include the authorization token in
 * swagger-ui
 */
(function () {
    console.log("custom code");
    const checkUiInterval = setInterval(() => {
        // Cerchiamo l'istanza di Swagger UI
        const ui = window.ui;
        if (ui) {
            clearInterval(checkUiInterval);
            overrideFetch(ui);
        }
    }, 100);

    function overrideFetch(ui) {
        const originalFetch = window.fetch;
        window.fetch = async (...args) => {
            const response = await originalFetch(...args);
            const url = args[0];
            if (url.includes('/api/auth/login') && response.ok) {
                const clone = response.clone();
                const data = await clone.json();

                // ADATTA QUI: controlla se il tuo token si chiama 'token', 'accessToken' o 'jwt'
                const token = data.accessToken;

                if (token) {
                    const auth = {
                        bearerAuth: {
                            name: "BearerAuth",
                            schema: {
                                type: "apiKey",
                                in: "header",
                                name: "Authorization",
                                value: "Bearer " + token
                            },
                            value: "Bearer " + token
                        }
                    };
                    ui.authActions.authorize(auth);
                    console.log("Swagger: Token iniettato automaticamente!");
                }
            }
            return response;
        };
    }
})();