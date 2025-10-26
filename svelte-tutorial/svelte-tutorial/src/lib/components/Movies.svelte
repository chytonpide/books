<script>
  export let data_temp = [];
  export let isModal = false;
  export let handleMovieId;
  export let handleLike;
</script>

<main>
  <h1>영화정보</h1>

  <div>
    {#each data_temp as movie, i}
      <div class="item">
        <figure>
          <img src="{movie.image}" alt=""/>
        </figure>
        <div class="info">
          <h3 class="bg-yellow">{movie.title}</h3>
          <p>{movie.year}</p>
          <p>{movie.category}</p>
          <button on:click={() => handleLike(movie.id)}>like {movie.like}</button>
          <button on:click={() => {
            handleMovieId(movie.id);
            isModal = true;
          }}
                  class="btn btn-primary">상세
          </button>
        </div>
      </div>
    {/each}
  </div>
</main>

<style>
  .bg-yellow {
    background: gold;
  }

  .item {
    width: 100%;
    border: 1px solid #ccc;
    display: flex;

  }

  .item figure {
    width: 30%;
    margin-right: 1rem;
  }

  .item img {
    width: 100%;
  }

  .item .info {
    width: 100%;
  }
</style>
