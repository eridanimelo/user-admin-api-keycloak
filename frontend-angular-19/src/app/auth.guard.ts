import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
    providedIn: 'root', // Para standalone
})
export class AuthGuard {
    constructor(private keycloakService: KeycloakService, private router: Router) { }

    canActivate: CanActivateFn = async (route, state) => {
        const isLoggedIn = await this.keycloakService.isLoggedIn();

        if (!isLoggedIn) {
            this.keycloakService.login({ redirectUri: window.location.origin + state.url });
            return false;
        }
        return true;
    };
}
