import 'package:dio/dio.dart';
import 'auth_service.dart';

class ApiService {
  final Dio _dio = Dio();
  final AuthService authService;

  ApiService(this.authService) {
    _dio.options.baseUrl = 'http://localhost:8080/api/users';
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await authService.getAccessToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
    ));
  }

  Future<void> createUser(Map<String, dynamic> userData) async {
    try {
      final userRepresentation = {
        'username': userData['username'],
        'email': userData['email'],
        'enabled': userData['enabled'] ?? true,
      };

      final requestPayload = {
        'user': userRepresentation,
        'password': userData['password'],
      };

      await _dio.post('/create', data: requestPayload);
    } catch (e) {
      throw Exception('Failed to create user: $e');
    }
  }

  Future<List<dynamic>> listUsers() async {
    try {
      final response = await _dio.get('/list');
      return response.data as List<dynamic>;
    } catch (e) {
      throw Exception('Failed to list users: $e');
    }
  }

  Future<void> resetPassword(String email, String newPassword) async {
    try {
      await _dio.post('/reset-password', data: {
        'email': email,
        'password': newPassword,
      });
    } catch (e) {
      throw Exception('Failed to reset password: $e');
    }
  }

  Future<void> deleteUser(String userId) async {
    try {
      await _dio.delete('/delete', queryParameters: {'userId': userId});
    } catch (e) {
      throw Exception('Failed to delete user: $e');
    }
  }

  Future<void> disableUser(String userId) async {
    try {
      await _dio.put('/disable', queryParameters: {'userId': userId});
    } catch (e) {
      throw Exception('Failed to disable user: $e');
    }
  }

  Future<void> enableUser(String userId) async {
    try {
      await _dio.put('/enable', queryParameters: {'userId': userId});
    } catch (e) {
      throw Exception('Failed to enable user: $e');
    }
  }

  Future<List<dynamic>> listRoles() async {
    try {
      final response = await _dio.get('/roles');
      return response.data as List<dynamic>;
    } catch (e) {
      throw Exception('Failed to list roles: $e');
    }
  }

  Future<void> addUserRole(String userId, String roleName) async {
    try {
      await _dio.post('/$userId/roles/add', queryParameters: {'roleName': roleName});
    } catch (e) {
      throw Exception('Failed to add role: $e');
    }
  }

  Future<void> removeUserRole(String userId, String roleName) async {
    try {
      await _dio.post('/$userId/roles/remove', queryParameters: {'roleName': roleName});
    } catch (e) {
      throw Exception('Failed to remove role: $e');
    }
  }
}
