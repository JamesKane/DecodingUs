package models

/**
 * Represents the roles that a user can have.
 *
 * This enumeration class provides a set of predefined user roles:
 * - User: Represents a regular user.
 * - Admin: Represents an administrator user.
 * - Researcher: Represents a researcher user.
 *
 * Each user role is associated with a numerical value used for comparison and identification purposes.
 *
 * Example usage:
 * {{{
 * // Declare a variable with the User role
 * val role: UserRoles = UserRoles.User
 *
 * // Access the value associated with the role
 * val roleValue: Int = role.value
 *
 * // Compare two roles
 * val isUser: Boolean = role == UserRoles.User
 * val isAdmin: Boolean = role == UserRoles.Admin
 * }}}
 */
enum UserRoles(val value: Int) {
  case User extends UserRoles(0)
  case Admin extends UserRoles(1)
  case Researcher extends UserRoles(2)
}

object UserRoles {
  def apply(value: Int): UserRoles = value match {
    case 0 => User
    case 1 => Admin
    case 2 => Researcher
    case _ => throw new IllegalArgumentException("Invalid role value")
  }
}