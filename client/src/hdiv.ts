
export interface SecureIdentifiable {
  nid?: string;
  id: string;
}

const regex = new RegExp('(.+)-([0-9a-fA-F]{3})-(.{8}-([0-9a-fA-FU]{1,33})-\\d+-.+)');

const Hdiv = {
  nid(id: string) {
    return id.substring(0, id.indexOf('-'));
  },

  isHid(hid: string) {
    return regex.test(hid);
  }
};

export default Hdiv;
