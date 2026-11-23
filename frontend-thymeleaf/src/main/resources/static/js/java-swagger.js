(() => {
    'use strict';
    window.addEventListener('DOMContentLoaded', () => {
        window.ui = SwaggerUIBundle({
            url: '/v3/api-docs',
            dom_id: '#swagger-ui',
            deepLinking: true,
            displayRequestDuration: true,
            docExpansion: 'list',
            defaultModelsExpandDepth: -1,
            validatorUrl: null,
            queryConfigEnabled: false,
            persistAuthorization: false,
            presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
            layout: 'StandaloneLayout'
        });
    });
})();
