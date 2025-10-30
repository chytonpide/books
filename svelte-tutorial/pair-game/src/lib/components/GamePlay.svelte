<script>
  import GameInfo from "./GameInfo.svelte";
  import GameGrid from "./GameGrid.svelte";

  export let page;

</script>
<main>
  <GameInfo></GameInfo>
  <GameGrid bind:page></GameGrid>
  <button class='btn' on:click={()=>page='title'}>back to home</button>
</main>