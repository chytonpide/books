<script>
  import Header from "./Header.svelte";
  import Footer from "./Footer.svelte";
</script>

<Header/>
<main class="container">
  <video
      autoplay
      loop
      muted
      src="/video/sample.mp4"/>
</main>
<Footer/>

<style>
  video {
    width: 100%;
    aspect-ratio: 2.35/1;
    object-fit: cover;
    object-position: center;
  }

</style>