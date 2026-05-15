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
  return request.get(`/vouchers/my/${id}`);
}

/** 赠送卡券 */
export function giftVoucher(data) {
  return request.post('/vouchers/my/gift', data);
}

/** 撤销赠送 */
export function cancelGift(giftId) {
  return request.post(`/vouchers/my/gift/${giftId}/cancel`);
}

/** 收件箱 */
export function getGiftInbox() {
  return request.get('/vouchers/my/gift/inbox');
}

/** 发件箱 */
export function getGiftOutbox() {
  return request.get('/vouchers/my/gift/outbox');
}

/** 接收赠送 */
export function acceptGift(giftId) {
  return request.post(`/vouchers/my/gift/${giftId}/accept`);
}

/** 拒绝赠送 */
export function rejectGift(giftId) {
  return request.post(`/vouchers/my/gift/${giftId}/reject`);
}
