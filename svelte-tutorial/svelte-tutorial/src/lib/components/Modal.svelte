<script>
  export let data;
  export let selectedMovieId;
  export let closeModal;

  const movie = data.find((m) => {return m.id === selectedMovieId})
</script>

<div class="modal">
  <div class="inner">
    <h3>{movie.title}</h3>
    <p>
      {@html movie.desc}
    </p>
    <button on:click={closeModal} class="btn-close">닫기</button>
  </div>
</div>

<style>

  .modal {
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .modal .inner {
    background: #fff;
    width: 80%;
    padding: 20px;
    border-radius: 10px;

  }

</style>