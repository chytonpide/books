<script>

  export let onNextClick
  export let title
  export let scoreTitle
  export let score = 0
  export let nextButtonText
  export let onPrevClick
  export let prevButtonText
</script>

<div class="modal">
  <div class="modal-content">
    <h1>{title}</h1>
    <p class="bonus-score-title">{scoreTitle}</p>
    <p class="bonus-score">{score}</p>
    <button on:click={onNextClick} class="btn next">{nextButtonText}</button>
    <button on:click={onPrevClick} class="btn">{prevButtonText}</button>
  </div>
</div>

<style lang="scss">
  // 모달창 스타일
  .modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    display: grid;
    place-items: center;

    .modal-content {
      background: #fff;
      width: 400px;
      padding: 20px;
      border-radius: 30px;
      border: 10px solid #000;
      h1 { margin-bottom: 5px; }
      .bonus-score-title {
        font-size: 20px;
        color: #666;
      }
      .bonus-score {
        font-size: 36px;
        color: #f63030;
        margin-bottom: 20px;
      }
    }

    .btn {
      font-size: 24px;
      &.next { color: #f63030; }
    }
  }

</style>