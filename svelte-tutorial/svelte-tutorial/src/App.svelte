<script>
  import {data} from './lib/movies.js'

  import {onDestroy, onMount} from "svelte"
  import Navbar from "./lib/components/Navbar.svelte";
  import Modal from "./lib/components/Modal.svelte";
  import Movies from "./lib/components/Movies.svelte";
  import Event from "./lib/components/Event.svelte";
  import SearchBar from "./lib/components/SearchBar.svelte";

  const event_texts = ['event1', 'event2', 'event3'];
  let data_temp = [...data]

  const handleLike = (id) => {
    data.forEach(movie => {
      if (movie.id === id) {
        movie.like += 1;
      }
    });

    data_temp = data.filter(movie => {
      return data_temp.includes(movie);
    })
  }

  let isModal = false;
  let selectedMovieId = '';

  const closeModal = () => {
    isModal = false;
  }

  const handleMovieId = (movieId) => {
    selectedMovieId = movieId;
  }

  let isEvent = true;
  let eventIndex = 0;
  let alertText = '';

  let interval
  let timeout

  $: {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      eventIndex += 1;
      if (eventIndex >= 3) {
        eventIndex = 0;
      }
    }, 3000);
    /*
    clearInterval(interval);

    interval = setInterval(() => {
      eventIndex += 1;
      if (eventIndex >= 3) {
        eventIndex = 0;
      }
    }, 3000);*/
  }


</script>
<Navbar/>
{#if isEvent}
  <Event bind:isEvent {event_texts} {eventIndex}/>
{/if}
<div class="container">
  <button on:click={() => isEvent=true}>이벤트열기</button>
</div>

<SearchBar {data} bind:data_temp bind:alertText/>
<div class="container">
  <button on:click={() => {
    data_temp = [...data];
    alertText = '';
  }}>전체보기
  </button>
</div>
<Movies {data_temp} bind:isModal {handleMovieId} {handleLike}/>
{#if isModal}
  <Modal data={data} {selectedMovieId} closeModal={closeModal}/>
{/if}

<style>
  .container {
    padding: 1rem;
    text-align: center;
  }

</style>
