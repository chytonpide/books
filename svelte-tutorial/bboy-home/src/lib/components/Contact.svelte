<script>

  import Header from "./Header.svelte";
  import Footer from "./Footer.svelte";
</script>

<Header></Header>
<main class="container">
  <h2>최고의 무대를 선사하겠습니다.</h2>
  <div class="contact">
    <figure class="map">
      <iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d13074.614805816374!2d135.7615772!3d34.990337950000004!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x600108a30d73c00f%3A0xe157e170ee12e45d!2sGoj%C5%8D%20Station!5e0!3m2!1sen!2skr!4v1761481716811!5m2!1sen!2skr" width="100%" height="450" style="border:0;" allowfullscreen="" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe>
    </figure>
    <address>
      <p>addr1</p>
      <p>addr2</p>
      <p>Tel<a href="tel:000-000-0000">000-000-0000</a></p>


    </address>

  </div>
</main>
<Footer></Footer>

<style lang="scss">
  .contact {
    display: flex;
    gap: 2rem;
    figure, address {
      width: 50%;
    }
  }

  address {
    font-style: normal;
  }

  a {
    color: inherit;
  }

</style>