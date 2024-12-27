import { HttpInterceptorFn } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { inject } from '@angular/core';
import { from } from 'rxjs';
import { switchMap } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const keycloakService = inject(KeycloakService);
    return from(keycloakService.getToken()).pipe(
        switchMap((token) => {
            const clonedRequest = req.clone({
                setHeaders: {
                    Authorization: token ? `Bearer ${token}` : '',
                },
            });
            return next(clonedRequest);
        })
    );
};
