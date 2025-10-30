<script>
  import { score, round, time } from "../../store/store.js";
  export let page;



  const highScore = localStorage.getItem("highScore") || 0;

  const lastScore = localStorage.getItem("lastScore") || 0;


</script>
<main>
  <h1 class="itim-regular">game score</h1>
  <div class="score">
    <p class="hi-score">Hi score:<span>{highScore}</span></p>
    <p class="your-score">Your last score:<span>{lastScore}</span></p>
  </div>
  <button class="btn itim-regular" on:click={()=>page='title'}>back to home</button>
</main>

<style lang="scss">
  main {
    h1 {
      font-size: 12vw;
      color: #ff3e00;
      -webkit-text-stroke: 10px #000;
      paint-order: stroke fill;
    }

    .score {
      font-size: 36px;
      font-weight: bold;
      margin: 70px 0;

      p {
        margin-bottom: 10px;
      }
    }
  }

</style>