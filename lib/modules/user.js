// TODO refreshToken property
// TODO reload() method
export default class User {
  constructor(authClass, authObj) {
    this._auth = authClass;
    this._user = null;
    this._updateValues(authObj);
  }

  /**
   * INTERNALS
   */

  /**
   *
   * @param authObj
   * @private
   */
  _updateValues(authObj) {
    this._authObj = authObj;
    if (authObj.user) {
      this._user = authObj.user;
    } else {
      this._user = null;
    }
  }

  /**
   * Returns a user property or null if does not exist
   * @param prop
   * @returns {*}
   * @private
   */
  _valueOrNull(prop) {
    if (!this._user) return null;
    if (!Object.hasOwnProperty.call(this._user, prop)) return null;
    return this._user[prop];
  }

  /**
   * PROPERTIES
   */

  get displayName() {
    return this._valueOrNull('displayName');
  }

  get email() {
    return this._valueOrNull('email');
  }

  get emailVerified() {
    return this._valueOrNull('emailVerified');
  }

  get isAnonymous() {
    return !this._valueOrNull('email') && this._valueOrNull('providerId') === 'firebase';
  }

  get photoURL() {
    return this._valueOrNull('photoURL');
  }

  get photoUrl() {
    return this._valueOrNull('photoURL');
  }

  // TODO no android method yet, the SDK does have .getProviderData but returns as a List.
  // get providerData() {
  //   return this._valueOrNull('providerData');
  // }

  get providerId() {
    return this._valueOrNull('providerId');
  }

  // TODO no android method?
  // get refreshToken() {
  //   return this._valueOrNull('refreshToken');
  // }

  get uid() {
    return this._valueOrNull('uid');
  }

  // noinspection ReservedWordAsName
  /**
   * METHODS
   */

  delete(...args) {
    return this._auth.deleteUser(...args);
  }

  // TODO valueOrNul token - optional promise
  getToken(...args) {
    return this._auth.getToken(...args);
  }

  get reauthenticate() {
    return this._auth.reauthenticateUser;
  }

  // TODO match errors to auth/something errors from firebase web api
  get updateEmail() {
    if (this.isAnonymous) return () => Promise.reject(new Error('Can not update email on an anonymous user.'));
    return this._auth.updateEmail;
  }

  get updateProfile() {
    return this._auth.updateProfile;
  }

  get updatePassword() {
    if (this.isAnonymous) return () => Promise.reject(new Error('Can not update password on an anonymous user.'));
    return this._auth.updatePassword;
  }

  get sendEmailVerification() {
    if (this.isAnonymous) return () => Promise.reject(new Error('Can not verify email on an anonymous user.'));
    return this._auth.sendEmailVerification;
  }
}
