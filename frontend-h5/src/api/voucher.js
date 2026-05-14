import request from './request';

/**
 * Employee login
 * @param {string} employeeId - 工号
 * @param {string} password - 密码
 * @returns {Promise}
 */
export function login(employeeId, password) {
  return request.post('/auth/login', { employeeId, password });
}

/**
 * Get my voucher list
 * @param {Object} params - { page, size, status }
 * @returns {Promise}
 */
export function getMyVouchers(params = {}) {
  return request.get('/vouchers/my', { params });
}

/**
 * Get voucher detail
 * @param {number|string} id - voucher ID
 * @returns {Promise}
 */
export function getVoucherDetail(id) {
  return request.get(`/vouchers/${id}`);
}
