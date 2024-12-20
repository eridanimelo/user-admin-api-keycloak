import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { AuthGuard } from './auth.guard';


export const routes: Routes = [
    { path: '', component: HomeComponent, canActivate: [AuthGuard] },
    { path: 'user-management', component: UserManagementComponent, canActivate: [AuthGuard] }
];
