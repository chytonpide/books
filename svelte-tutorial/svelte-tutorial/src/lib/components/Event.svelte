<script>
  import {fade, fly} from 'svelte/transition'

  export let isEvent = true
  export let event_texts = []
  export let eventIndex = 0

</script>
<div class="event show" in:fly={{ y: -400, duration: 1000}} out:fade>
  <p>{event_texts[eventIndex]}</p>
  <button on:click={()=> isEvent = false}>X</button>
</div>

<style>
  .event {
    width: 100%;
    background: #666;
    padding: 5px 1em;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #fff;
    text-align: center;
    margin-bottom: 1em;

    max-height: 0;
    opacity: 0;
    overflow: hidden;
    transition: all 0.4s;
  }

  .show {
    opacity: 1;
    max-height: 100%;
  }

  .event button {
    padding: 2px;
  }


</style>
