import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:user_manegement/auth_service.dart';
import 'auth_event.dart';
import 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthService _authService = AuthService();

  AuthBloc() : super(AuthInitial()) {
    on<LoginRequested>(_onLoginRequested);
    on<LogoutRequested>(_onLogoutRequested);
  }

  AuthService get authService => _authService; // Adicionado este getter.

  Future<void> _onLoginRequested(LoginRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      await _authService.authenticate();
      emit(Authenticated());
    } catch (e) {
      emit(AuthError('Login failed: $e'));
    }
  }

  Future<void> _onLogoutRequested(LogoutRequested event, Emitter<AuthState> emit) async {
    await _authService.logout();
    emit(AuthInitial());
  }
}
