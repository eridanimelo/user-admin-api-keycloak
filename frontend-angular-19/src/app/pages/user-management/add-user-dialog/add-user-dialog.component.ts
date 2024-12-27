import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { UserService } from '../../../services/user.service';
import { ToastrService } from 'ngx-toastr';
import { UserRequestDTO, UserRepresentation } from '../../../services/user.service';

@Component({
  selector: 'app-add-user-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './add-user-dialog.component.html',
  styleUrls: ['./add-user-dialog.component.css'],
})
export class AddUserDialogComponent {
  userForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService,
    private dialogRef: MatDialogRef<AddUserDialogComponent>
  ) {
    this.userForm = this.fb.group({
      username: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required],
    });
  }

  createUser(): void {
    if (this.userForm.valid) {
      const formValues = this.userForm.value;

      // Criando o UserRepresentation
      const userRepresentation: UserRepresentation = {
        username: formValues.username,
        firstName: formValues.firstName,
        lastName: formValues.lastName,
        email: formValues.email,
      };

      // Criando o UserRequestDTO
      const userRequest: UserRequestDTO = {
        user: userRepresentation,
        password: formValues.password,
      };

      this.userService.createUser(userRequest).subscribe({
        next: () => {
          this.toastr.success('User created successfully!', 'Success');
          this.dialogRef.close(userRequest);
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Error creating user.', 'Error');
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
