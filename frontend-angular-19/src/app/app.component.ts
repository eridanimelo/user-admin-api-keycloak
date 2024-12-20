import { Component } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';

import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatSidenavModule } from '@angular/material/sidenav';

import { CommonModule } from '@angular/common';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, RouterModule, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule, MatSidenavModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend-angular-19';

  username: string | undefined;

  constructor(private keycloakService: KeycloakService) { }

  menuItems = [
    { name: 'Home', route: '/' },
    { name: 'User Management', route: '/user-management' },
  ];

  async ngOnInit() {
    const userDetails = await this.keycloakService.loadUserProfile();
    this.username = userDetails.username;
  }

  async onLogout(): Promise<void> {
    try {
      await this.keycloakService.logout('http://localhost:4200');
    } catch (error) {
      console.error('Logout failed', error);
    }
  }

}
