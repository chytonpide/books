<script>

  import Header from "./Header.svelte";
  import Footer from "./Footer.svelte";
  import {news} from "../../assets/news.js";

  export let params = {};
  console.log(params);
  const selectedNews = news.find(n => n.id === params.id)
</script>

<Header></Header>
<main class="container">
  <div class="main-header">
    <h2>{selectedNews.title}</h2>

    <div class="date">{selectedNews.date}</div>
    <img src="{selectedNews.imgUrl}" alt=""/>
  </div>
  <article>
    {@html selectedNews.desc}
  </article>
</main>
<Footer></Footer>

<style lang="scss">
  .main-header {
    h2 {
      margin-bottom: 1em;
    }

    .date {
      font-size: 15px;
      color: #999;
      margin-bottom: 2em;
    }

    img {
      width: 100%;
    }
  }

  article {
    margin-top: 2rem;
  }


</style>