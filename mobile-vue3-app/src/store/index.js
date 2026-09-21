import { createStore } from 'vuex';

export default createStore({
  state: {
    qrcode: '',
    roleName: localStorage.getItem('roleName') || '', // 角色名称，从 localStorage 恢复
    relocatedLocation: null, // 重新定位后的位置信息 { longitude, latitude, specificLocation }
    feedbackFormData: null, // 反馈表单数据 { currentIndex, formData, uploadResults }
    feedbackFileList: null // 反馈文件列表（保存可序列化的文件信息）
  },
  mutations: {
    setQrcode(state, qrcode) {
      state.qrcode = qrcode;
    },
    setRoleName(state, roleName) {
      state.roleName = roleName;
    },
    setRelocatedLocation(state, location) {
      state.relocatedLocation = location;
    },
    clearRelocatedLocation(state) {
      state.relocatedLocation = null;
    },
    setFeedbackFormData(state, formData) {
      state.feedbackFormData = formData;
    },
    clearFeedbackFormData(state) {
      state.feedbackFormData = null;
    },
    setFeedbackFileList(state, fileList) {
      state.feedbackFileList = fileList;
    },
    clearFeedbackFileList(state) {
      state.feedbackFileList = null;
    }
  },
  actions: {
    updateQrcode({ commit }, qrcode) {
      commit('setQrcode', qrcode);
    },
    updateRoleName({ commit }, roleName) {
      commit('setRoleName', roleName);
    },
    updateRelocatedLocation({ commit }, location) {
      commit('setRelocatedLocation', location);
    },
    clearRelocatedLocation({ commit }) {
      commit('clearRelocatedLocation');
    },
    updateFeedbackFormData({ commit }, formData) {
      commit('setFeedbackFormData', formData);
    },
    clearFeedbackFormData({ commit }) {
      commit('clearFeedbackFormData');
    },
    updateFeedbackFileList({ commit }, fileList) {
      commit('setFeedbackFileList', fileList);
    },
    clearFeedbackFileList({ commit }) {
      commit('clearFeedbackFileList');
    }
  },
  getters: {
    getQrcode: (state) => state.qrcode,
    getRoleName: (state) => state.roleName,
    getRelocatedLocation: (state) => state.relocatedLocation,
    getFeedbackFormData: (state) => state.feedbackFormData,
    getFeedbackFileList: (state) => state.feedbackFileList
  }
});

