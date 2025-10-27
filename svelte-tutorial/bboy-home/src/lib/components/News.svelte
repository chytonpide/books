<script>

  import Header from "./Header.svelte";
  import Footer from "./Footer.svelte";
  import {news} from "../../assets/news.js";

  console.log(news);
</script>

<Header></Header>
<main class="container">
  <h2>비보이 브라더스 활동</h2>
  <div class="news">
    {#each news as item, i}
      <a class="news-item" href="#/detail/{item.id}">
        <img src="/image/sample.png" alt=""/>
        <h3>{item.title}</h3>
        <p class="date">{item.date}</p>
        <div class="hashtag">
          {#each item.hashtag as hashtagItem}
            <span>#{hashtagItem}</span>&nbsp;
          {/each}
        </div>
      </a>
    {/each}
  </div>
</main>
<Footer></Footer>

<style lang="scss">
  .news {
    display: flex;
    gap: 1rem;

    .news-item {
      width: calc(100% / 3);

      img {
        width: 100%;
      }

      box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.1);

      .item-text {
        padding: 20px;
      }
    }


    h3 {
      margin-bottom: 1rem;
    }

    p {
      margin-bottom: 1rem;
    }

    .date, .hashtag {
      font-size: 15px;
      color: #666;
    }

    a {
      color: inherit;
      text-decoration: none;
    }
  }


</style>