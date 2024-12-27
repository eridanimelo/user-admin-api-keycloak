import { KeycloakService } from 'keycloak-angular';

export function initializeKeycloak(keycloak: KeycloakService) {
    return (): Promise<any> => {
        // Check if we are on the client-side (browser)
        if (typeof window !== 'undefined') {
            return keycloak.init({
                config: {
                    url: 'http://localhost:8081', // URL of the Keycloak server
                    realm: 'user-api',           // Name of the Realm
                    clientId: 'userapi',         // Client ID configured in Keycloak
                },
                initOptions: {
                    onLoad: 'login-required',   // Forces login if not authenticated
                    checkLoginIframe: false,    // Disables the login iframe check
                },
                enableBearerInterceptor: true, // Enables the token interceptor for HTTP requests
                bearerExcludedUrls: ['/assets', '/public'], // URLs that are excluded from token verification (public assets)
            });
        } else {
            return Promise.resolve(); // If it's on the server side, don't initialize Keycloak (for SSR scenarios)
        }
    };
}
