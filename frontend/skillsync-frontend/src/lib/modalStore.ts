import { writable } from 'svelte/store';

type ModalState = {
  open: boolean;
  title: string;
  message: string;
  _resolve?: (v:boolean)=>void;
};

const { subscribe, set, update } = writable<ModalState>({ open: false, title: '', message: '' });

function confirm(message: string, title = 'Please confirm') {
  return new Promise<boolean>((resolve) => {
    update(s => ({ ...s, open: true, title, message, _resolve: resolve }));
  });
}

function close() {
  update(s => ({ ...s, open: false }));
}

export default {
  subscribe,
  confirm,
  close,
};
