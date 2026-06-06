<template>
  <footer class="footer">
    <div class="container footer-content">
      <div class="footer-section">
        <h3>🎨 手作空间</h3>
        <p>发现手工之美，分享创作乐趣</p>
      </div>
      <div class="footer-section">
        <h4>快速链接</h4>
        <router-link to="/">首页</router-link>
        <router-link to="/publish">发布作品</router-link>
        <router-link to="/favorites">我的收藏</router-link>
      </div>
      <div class="footer-section">
        <h4>手作品类</h4>
        <router-link to="/category/1">编织</router-link>
        <router-link to="/category/2">陶艺</router-link>
        <router-link to="/category/3">布艺</router-link>
        <router-link to="/category/4">木艺</router-link>
      </div>
      <div class="footer-section">
        <h4>关于我们</h4>
        <span>平台公约</span>
        <span>联系我们</span>
        <span>意见反馈</span>
      </div>
    </div>
    <div class="copyright">
      <p>© 2024 手作空间 Handmade Studio. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  background: #2d3748;
  color: #a0aec0;
  padding: 40px 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section h3 {
  color: #fff;
  font-size: 20px;
  margin-bottom: 16px;
}

.footer-section h4 {
  color: #fff;
  font-size: 16px;
  margin-bottom: 16px;
}

.footer-section p {
  line-height: 1.6;
  font-size: 14px;
}

.footer-section a,
.footer-section span {
  display: block;
  margin-bottom: 10px;
  color: #a0aec0;
  cursor: pointer;
  transition: color 0.3s;
  font-size: 14px;
}

.footer-section a:hover,
.footer-section span:hover {
  color: #667eea;
}

.copyright {
  border-top: 1px solid #4a5568;
  padding-top: 20px;
  text-align: center;
  font-size: 13px;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }
}
</style>
