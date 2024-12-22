import 'package:flutter_appauth/flutter_appauth.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class AuthService {
  final FlutterAppAuth _appAuth = const FlutterAppAuth();
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  final String _clientId = 'userapi';
  final String _issuer = 'http://localhost:8081/realms/user-api';
  final String _redirectUrl = 'com.eridanimelo.userapi://login-callback';
  final List<String> _scopes = ['openid', 'profile', 'email'];

  Future<void> authenticate() async {
    try {
      final result = await _appAuth.authorizeAndExchangeCode(
        AuthorizationTokenRequest(
          _clientId,
          _redirectUrl,
          issuer: _issuer,
          scopes: _scopes,
        ),
      );

      if (result != null) {
        await _secureStorage.write(key: 'access_token', value: result.accessToken);
        await _secureStorage.write(key: 'refresh_token', value: result.refreshToken);
      }
    } catch (e) {
      throw Exception('Authentication failed: $e');
    }
  }

  Future<String?> getAccessToken() async {
    return _secureStorage.read(key: 'access_token');
  }

  Future<void> logout() async {
    await _secureStorage.deleteAll();
  }
}
